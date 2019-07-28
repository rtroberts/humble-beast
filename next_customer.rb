#!/usr/bin/env ruby
require 'date'
require 'mechanize'
require 'json'
require 'logger'
require 'shopify_api'
require 'pp'

@base_url = "https://humblebeast.myshopify.com"
@login_username = ""
@login_password = ""
@cutoff_date = DateTime.new(2015,12,24)
@auth_token = ""
API_KEY = ''
PASSWORD = ''
shop_url = "https://#{API_KEY}:#{PASSWORD}@humblebeast.myshopify.com/admin"
ShopifyAPI::Base.site = shop_url


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

def create_discount
  start_date = DateTime.now
  end_date = start_date + 7
  discount_title = "SHP-#{[*('A'..'Z'),*('0'..'9')].shuffle[0,8].join}"
  result = @agent.post("#{@base_url}/admin/discounts", {
    "authenticity_token" => @auth_token,
    "utf8" => "✓",
    'discount[code]' => discount_title,
    'discount[value]' => '',
    'discount[discount_type]' => 'shipping',
    'discount[applies_to_resource]' => 'country',
    'discount[applies_to_id]' => '189389638',
    'discount[starts_at]' => start_date.strftime("%Y-%m-%d"), 
    'discount[ends_at]' => end_date.strftime("%Y-%m-%d"),
    #'discount[minimum_order_amount]' => '0.00',
    'discount[usage_limit]' => '1',
  })
  puts "Unsuccessful discount post" unless result.code == "200"
  (result.code == "200") ? discount_title : nil
end

# Get all the recent orders eligible for whatsits
def get_eligible
  #older than 1 day, not older than X date (whenever they get these cards).
end

# Upon successful email, add the discount code to the note order.
def append_order_note(order_num, discount_code)
end


def send_email(checkout_url, customer_name, discount_code)
end

@auth_token = login
get_eligible.each do |user|
  # The create_discount call HAS to work, otherwise we should skip it and try again later
  #send_email(user[:url], user[:customer], create_discount)
end
