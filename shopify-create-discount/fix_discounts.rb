#!/usr/bin/env ruby
require 'date'
require 'mechanize'
require 'json'
require 'logger'
require 'shopify_api'
require 'pp'
require 'gmail'

### Production
@base_url = "https://humblebeast.myshopify.com"
@login_username = ""
@login_password = ""
API_KEY = ''
PASSWORD = ''
shop_url = "https://#{API_KEY}:#{PASSWORD}@humblebeast.myshopify.com/admin"


ShopifyAPI::Base.site = shop_url

@auth_token = ""
@start_date = DateTime.new(2016,3,16,0,0,0,'-8')
puts "Starting up... #{DateTime.now}"

@agent = Mechanize.new  #{|a| a.log = Logger.new(STDERR) }

def login
  signin = @agent.get "#{@base_url}/admin/auth/login"
  login_form = signin.form_with :action => "/admin/auth/login"
  login_form.field_with(:name => "login").value = @login_username
  login_form.field_with(:name => "password").value = @login_password
  result = @agent.submit login_form
  result.forms.first.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
end

def create_discount(code)
  start_date = DateTime.now
  end_date = start_date + 32
  result = @agent.post("#{@base_url}/admin/discounts", {
    "authenticity_token" => @auth_token,
    "utf8" => "✓",
    'discount[code]' => code,
    'discount[value]' => '20',
    'discount[discount_type]' => 'percentage',
    'discount[applies_to_resource]' => '',
    ##'discount[applies_to_id]' => '189389638', 
    'discount[starts_at]' => start_date.strftime("%Y-%m-%d"), 
    'discount[ends_at]' => end_date.strftime("%Y-%m-%d"),
    #'discount[minimum_order_amount]' => '0.00',
    'discount[usage_limit]' => '1',
    })
  puts "Unsuccessful discount post" unless result.code == "200"
  (result.code == "200") ? code : nil
end

def has_discount_code?(note_text)
  # Nonmatches return nil, so the lack of a match should 
  # indicate this will return true
  !!(/discount_code=HBEP-[0-9A-Z]{8}/.match(note_text))
end

# Get all the recent orders eligible for whatsits
def get_eligible
  yesterday = DateTime.now - 1
  orders = ShopifyAPI::Order.all
  # Orders which have been fulfilled recently.
  orders.select do |order|
    order.fulfillment_status && 
    order.fulfillments &&
    order.financial_status == "paid" &&
    order.fulfillments.first.status == "success" && 
    order.fulfillments.first.updated_at < yesterday && 
    order.fulfillments.first.updated_at >= @start_date &&
    has_discount_code?(order.note)
  end
end

@auth_token = login
get_eligible.each do |order|
  puts "Eligible order: #{order.id}"
  code = /discount_code=(HBEP-[0-9A-Z]{8})/.match(order.note)[1]
  resp = create_discount(code)
  unless resp
    puts "Error: code creation failed for order id #{order.id}"
    next
  end
  puts "Created code #{code} for customer #{order.id}"
end
