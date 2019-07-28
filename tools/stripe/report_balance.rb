#!/usr/bin/env ruby

require 'stripe'
require 'mechanize'
# Get keys from gitignored file (which just contains an array of hashes named 'accounts')
require_relative '.stripe_keys'
require_relative '.shopify_keys'

# Unix timestamp 24hrs ago.
#yesterday = Time.now
date = Time.now.to_i - 86400
@shopify_date = Time.at(date).to_date
puts "Shopify date is #{@shopify_date}"

@agent = Mechanize.new  #{|a| a.log = Logger.new(STDERR) }
stripe_accounts = StripeAccounts
shopify_accounts = ShopifyAccounts

def our_sum(list_o_values)
  list_o_values.inject(0) { |sum, p| sum + p.amount }
end

def format(account_name, total)
  line_length = 24
  formatted_total = total.to_s.gsub(/(.*)(.{2})/, '\1.\2')
  spaces = line_length - (account_name.length + formatted_total.length) - 1
  "#{account_name}\t#{" " * spaces}$#{formatted_total}"
end

def login(base_url, username, password)
  signin = @agent.get "#{base_url}/admin/auth/login"
  login_form = signin.form_with :action => "/admin/auth/login"
  login_form.field_with(:name => "login").value = username
  login_form.field_with(:name => "password").value = password
  result = @agent.submit login_form
  login_form.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
end

def get_payout(base_url)
  results = @agent.get("#{base_url}/admin/payments/payouts")
  hashes = [] 
  # Get expired status, too
  results.search("//*[@id='transfers-results']/div[1]/div/table/tbody/tr").collect do |row|
    detail = {}
    [
      [:payout_date, 'td[1]/a/text()'],
      [:status, 'td[2]/span/text()'],
      [:amount, 'td[7]/text()'],
    ].each do |name, xpath|
      detail[name] = row.at_xpath(xpath).to_s.strip
    end

    #detail[:start_obj] = DateTime.parse("#{detail[:start_date]}-0700")
    #detail[:end_obj] = DateTime.parse("#{detail[:end_date]}-0700")
    hashes << detail
  end

  hashes.select { |x| Date.parse(x[:payout_date]) == @shopify_date }.first[:amount]
end


stripe_accounts.each do |x|
  Stripe.api_key = x[:account_key]
  charges = Stripe::Charge.all(:created => {:gte => date}).data
  refunds = Stripe::Refund.all(:created => {:gte => date}).data
  revenue = our_sum(charges)
  debits = our_sum(refunds)
  shopify_payment = 0
  # If a shopify shop exists...
  shop = shopify_accounts.select {|y| y[:account_name] == x[:account_name]}.first
  if shop
    base_url = "https://#{shop[:shop_name]}.myshopify.com"
    login(base_url, shop[:username], shop[:password])
    shopify_payment = get_payout(base_url).gsub(/\$/, '').gsub(/\./, '').to_i
  end
  x[:total] = revenue - debits + shopify_payment
end

# Add our total to the list of hashes
stripe_accounts << {:account_name => "Total: ",
                    :total => stripe_accounts.collect {|x| x[:total]}.inject(0) {|sum, p| sum + p}
}

report = stripe_accounts.map do |x|
  format(x[:account_name], x[:total])
end

# Post to Slack boiii
`curl -sX POST --data-urlencode 'payload={"channel": "#stripe-notifications", "username": "stripeBalanceBot", "text": "\nNet revenue in the past 24 hours: \n\`\`\`#{report.join('\n')}\`\`\`", "icon_emoji": ":money_with_wings:"}' https://hooks.slack.com/services/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`

