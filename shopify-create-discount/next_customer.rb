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
@start_date = DateTime.new(2016,3,8,0,0,0,'-8')
puts "Starting up... #{DateTime.now}"
@email_body = "<p>Thanks for your recent support to Humble Beast!</p>
<p>Because our largest potential revenue stream is offered to our listeners and fans at no charge, merchandise goes a long way to keeping our operations running smoothly and our loyal staff paid. </p>

<p>To show our appreciation, <b>if you make a purchase at our online store in the next 30 days, we will give you 20% off!</b></p>

<p>Just use this code at checkout: </p>
DISCOUNT_CODE_HERE
<p>Thank you again for your immense support!</p>

<p>Regards,</p>

<p>The Humble Beast Family</p>"

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
  end_date = start_date + 32
  discount_title = "HBEP-#{[*('A'..'Z'),*('0'..'9')].shuffle[0,8].join}"
  result = @agent.post("#{@base_url}/admin/discounts", {
    "authenticity_token" => @auth_token,
    "utf8" => "✓",
    'discount[code]' => discount_title,
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
  (result.code == "200") ? discount_title : nil
end

def no_discount_code?(note_text)
  # Nonmatches return nil, so the lack of a match should 
  # indicate this will return true
  !(/discount_code=HBEP-[0-9A-Z]{8}/.match(note_text))
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
    no_discount_code?(order.note)
  end
end

# Upon successful email, add the discount code to the note order.
def append_order_note(order, discount_code)
  oldnote = order.note
  order.note = "discount_code=#{discount_code}\n#{oldnote}"
  order.save
end

def make_email_body(discount_code)
  @email_body.gsub(/DISCOUNT_CODE_HERE/, "<h3><b>#{discount_code}</b></h3>")
end

def send_email(customer_email, discount_code)
  email_body = make_email_body(discount_code)
  Gmail.new('support@humblebeast.com', 'Humility738') do |gmail|
    return gmail.deliver do
      to customer_email
      subject "Thanks for your support! 20% off your next order!"
      html_part do
        content_type 'text/html; charset=UTF-8'
        body email_body
      end
    end
  end
end

@auth_token = login
get_eligible.each do |order|
  puts "Eligible order: #{order.id}"
  code = create_discount
  unless code
    puts "Error: code creation failed for order id #{order.id}"
    next
  end
  status = send_email(order.email, code) 
  append_order_note(order, code) if status
end
