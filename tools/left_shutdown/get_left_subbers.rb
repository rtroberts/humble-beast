#!/usr/bin/env ruby

require 'stripe'
#require 'mechanize'
# Get keys from gitignored file (which just contains an array of hashes named 'accounts')
require_relative '.stripe_keys'
require_relative '.shopify_keys'

stripe_accounts = StripeAccounts
@customers = []
left = stripe_accounts.select {|x| x[:account_name] == "Left Roasters"}
left.each do |x|
  puts "ACCOUNT: #{left}"
  Stripe.api_key = x[:account_key]
  while true
    if @customers.empty?
      list = Stripe::Customer.list(:limit => 100).data
    else 
      last_customer = @customers.last.id
      list = Stripe::Customer.list(:limit => 100, :starting_after => last_customer).data
    end
    @customers << list
    @customers.flatten!
    break if list.count < 100
    sleep 2
  end
end

emails = @customers.map {|x| x.email}.compact
puts emails
# Post to Slack boiii
#`curl -sX POST --data-urlencode 'payload={"channel": "#stripe-notifications", "username": "stripeBalanceBot", "text": "\nNet revenue in the past 24 hours: \n\`\`\`#{report.join('\n')}\`\`\`", "icon_emoji": ":money_with_wings:"}' https://hooks.slack.com/services/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`

