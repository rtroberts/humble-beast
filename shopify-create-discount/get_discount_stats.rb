#!/usr/bin/env ruby
require 'date'
require 'mechanize'
require 'json'
require 'logger'
require 'shopify_api'
require 'pp'
require 'gmail'

@base_url = "https://humblebeast.myshopify.com"
@login_username = ""
@login_password = ""

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


def get_discounts(query)
  next_page = true
  hashes = []
  url = "#{@base_url}/admin/discounts?limit=250&order=id+desc&query=#{query}"

  results = @agent.get(url)
  # Get expired status, too

  results.search("table[@id=discount-table]/tbody/tr").collect do |row|
    detail = {}
    [
      [:code, 'td[2]/div/span/text()'],
      [:used, 'td[3]/text()'],
      [:start_date, 'td[4]/span/text()'],
      [:end_date, 'td[5]/span/text()'],
    ].each do |name, xpath|
      detail[name] = row.at_xpath(xpath).to_s.strip
    end
    #detail[:start_obj] = DateTime.parse("#{detail[:start_date]}-0700")
    #detail[:end_obj] = DateTime.parse("#{detail[:end_date]}-0700")
    hashes << detail
  end
  hashes
end


def get_stats(data, discount_regex)
  baseline = data.select {|x| discount_regex.match(x[:code]) }
  used = baseline.select {|x| x[:used] == "1" }.count
  #  puts used
  puts "Usage percentage: #{used.to_f / baseline.count.to_f}"
end


@auth_token = login
puts "Shipping discount"
get_stats(get_discounts("SHP"), /SHP-/)
puts "20% off discount"
get_stats(get_discounts("HBEP"), /HBEP-/)

