#!/usr/bin/env ruby
require 'date'
require 'mechanize'
require 'json'
require 'logger'
require 'pp'

@base_url = "https://humblebeast.myshopify.com"
@login_username = ""
@login_password = ""
@auth_token = ""

@agent = Mechanize.new  #{|a| a.log = Logger.new(STDERR) }

def login
  signin = @agent.get "#{@base_url}/admin/auth/login"
  login_form = signin.form_with :action => "/admin/auth/login"
  login_form.field_with(:id => "login-input").value = @login_username
  login_form.field_with(:id => "password").value = @login_password
  result = @agent.submit login_form
  login_form.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
end

def create_discount(discount_code, rate)
  auth = @agent.get("#{@base_url}/admin/discounts/new")
  @auth_token = auth.forms.first.fields.select do |x|
    x.name == "authenticity_token"
  end.first.value
  result = @agent.post("#{@base_url}/admin/discounts", {
    "utf8" => "✓",
    "authenticity_token" => @auth_token,
    'discount[code]' => discount_code,
    'discount[discount_type]' => 'fixed_amount',
    'discount[value]' => rate,
    'discount[applies_to_resource]' => '',
    'usage_limit_type' => 'with_limit',
    'discount[usage_limit]' => '1',
    'discount[applies_once_per_customer]' => '0',
    'discount[starts_at]' => '2016-12-12',
    'discount_never_expires' => '',
  })
  puts "Unsuccessful discount post" unless result.code == "200"
  #pp result.header
  (result.code == "200") ? discount_code : nil
end

@auth_token = login
puts "Auth token: #{@auth_token.inspect}"
File.readlines("gift_cards.txt").each do |line|
  code, rate = line.split(",")
  puts "#{code}, #{rate.strip}"
  create_discount(code, rate.strip)
  sleep 1
end
