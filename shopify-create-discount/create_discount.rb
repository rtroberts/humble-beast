#!/usr/bin/env ruby
require 'date'
require 'mechanize'
require 'json'
require 'logger'
require 'pp'

@base_url = "https://humblebeast.myshopify.com"
@login_username = "redacted"
@login_password = "redacted"
@cutoff_date = DateTime.new(2015,12,24)
@auth_token = ""

@agent = Mechanize.new  #{|a| a.log = Logger.new(STDERR) }

def login
  signin = @agent.get "#{@base_url}/admin/auth/login"
  login_form = signin.form_with :action => "/admin/auth/login"
  login_form.field_with(:name => "login").value = @login_username
  login_form.field_with(:name => "password").value = @login_password
  result = @agent.submit login_form
  login_form.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
end

def create_discount
  start_date = DateTime.now
  end_date = start_date + 7
  discount_title = "SHP-#{[*('A'..'Z'),*('0'..'9')].shuffle[0,8].join}"
  auth = @agent.get("#{@base_url}/admin/discounts/new")
  @auth_token = auth.forms.first.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
  result = @agent.post("#{@base_url}/admin/discounts", {
    "authenticity_token" => @auth_token,
    "utf8" => "✓",
    'discount[code]' => discount_title,
    'discount[value]' => '',
    'discount[discount_type]' => 'shipping',
    'discount[applies_to_resource]' => 'country',
    'discount[applies_to_id]' => '11168089',
    'discount[starts_at]' => start_date.strftime("%Y-%m-%d"), 
    'discount[ends_at]' => end_date.strftime("%Y-%m-%d"),
    #'discount[minimum_order_amount]' => '0.00',
    'discount[usage_limit]' => '1',
  })
  puts "Unsuccessful discount post" unless result.code == "200"
  #pp result.header
  (result.code == "200") ? discount_title : nil
end

# Get all the carts eligible for emailing
def get_eligible
  # Get the list of abandoned carts which haven't sent an email; parse the HTML into understandable hashes
  results = @agent.get("#{@base_url}/admin/checkouts?direction=next&limit=250&order=created_at+desc&query=&abandoned_email_state=not_sent")
  #results = @agent.get("#{@base_url}/admin/checkouts?direction=next&limit=250&order=created_at+desc&query=&abandoned_email_state=sent")
  hashes = results.search("table[@id=all-checkouts]/tbody/tr").collect do |row|
    detail = {}
    [
      [:url, 'td[2]/a/@href'],
      [:date, 'td[3]/span/@title'],
      [:customer, 'td[4]/div/a/text()'],
      [:email_status, 'td[5]/span/text()'],
    ].each do |name, xpath|
      detail[name] = row.at_xpath(xpath).to_s.strip
    end
    # Parse the date string into an actual object
    detail[:date_obj] = DateTime.parse("#{detail[:date]}-0800")
    detail
  end
  # Return those hashes which are over a day old and are newer than the cutoff
  eligible = hashes.select do |x|
    ((x[:date_obj] + (6.0/24)) < DateTime.now) && (x[:date_obj] > @cutoff_date)
  end
  eligible.each {|x| puts "Customer #{x[:customer]} is eligible with ruby timestamp #{x[:date_obj]} and shopify date of #{x[:date]}"}
  eligible
end

# Parse and return user data from shopify API (need a couple extra things, like the callback url)
def get_user_data(checkout_url)
  JSON.parse(@agent.get("#{@base_url}#{checkout_url}.json").body)
end

# Create the HTML list of items
def generate_item_list(user)
  user["checkout"]["line_items"].map do |item|
    "#{item["quantity"]}x #{item["title"]}"
  end.join('\n')
end

def send_email(checkout_url, customer_name, discount_code)
  puts "send_email auth token #{@auth_token}"
  user = get_user_data(checkout_url)
  callback_url = user["checkout"]["abandoned_checkout_url"]
  email_content = <<-EOS
Disclaimer: Unfortunately, this only applies to shipping within the United States.


Hey #{customer_name},

We notice that you left your cart full and bailed on the store. We know you like what you picked, or you wouldn't have put it in your cart. Did you see the shipping charge and cringe?

In your cart, you left:

#{generate_item_list(user)}
    
How about we cover the shipping? We'll send you your purchase for free! Just head back to the store using the below link and discount code at checkout to get free shipping on this order. We really appreciate your support and don't want a small shipping fee to get in the way of that. Your code will be active for one (1) week. 
    
Thanks so much!

Humble Beast
    
Discount code: #{discount_code}
#{callback_url}
EOS

  puts "[#{DateTime.now}] sending email to user #{customer_name} (#{checkout_url}) with code #{discount_code}"
  puts "Email content: #{email_content}"
  auth = @agent.get("#{@base_url}#{checkout_url}")
  @auth_token = auth.forms.first.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
  result = @agent.post("#{@base_url}#{checkout_url}/contact", {
    "authenticity_token" => @auth_token,
    "utf8" => "✓",
    "source" => "adminnext", 
    "abandoned_checkout_contact_message[from]" => 'orders@humblebeast.com',
    #"abandoned_checkout_contact_message[from]" => 'ryan@larkso.me',
    "abandoned_checkout_contact_message[subject]" => "Complete your Purchase",
    "abandoned_checkout_contact_message[body]" => email_content,
    "_method" => "post"})
    puts "Email HTTP status: #{result.code}"
end

@auth_token = login
get_eligible.each do |user|
  send_email(user[:url], user[:customer], create_discount)
end
