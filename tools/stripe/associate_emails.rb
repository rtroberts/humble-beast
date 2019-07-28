#!/usr/bin/env ruby

require 'stripe'
require 'shopify_api'
require 'active_resource'
# Get keys from gitignored file (which just contains an array of hashes named 'accounts')
require_relative '.stripe_keys'
require_relative '.shopify_keys'

API_KEY = 'redacted'
PASSWORD = 'redacted'
shop_url = "https://#{API_KEY}:#{PASSWORD}@humblebeast.myshopify.com/admin"

ShopifyAPI::Base.site = shop_url

def wrap_item(x)
  "\"#{x}\""
end

def format_address(x)
  r = x.default_address
  return nil unless r
  result = [x.email, r&.address1, r&.address2, r&.city, r&.province, r&.country, r&.zip]
  result.map {|x| x.include?(" ") ? wrap_item(x) : x }.flatten
end

def check_for_email(email)
  result = ShopifyAPI::Customer.search(query: email)
  iterate = result.first
  return nil unless iterate
  begin
  format_address(iterate)
  rescue Exception => e
  #puts e
  return nil
  end
end

headers = ["stripe_email", "shopify_email", "address1", "address2", "city", "province", "country", "zip"]
results = []
results << headers
File.readlines("customers.csv").each do |line|
  ll = line.split(',')
  email = ll[2]
  meta = ll[31]
  next unless meta.include? "Monthly Support for Humble Beast"
  results << [email, check_for_email(email)].flatten
end

results.each do |line|
  puts line.join(',')
end
