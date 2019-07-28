#!/usr/bin/env ruby

require 'watir-webdriver'
b = Watir::Browser.new :chrome
url = 'http://clichesite.com/alpha_list.asp?which=lett+'
cliches = []
25.times do |num|
  b.goto "#{url}#{num}"
  table = b.table(:xpath => '/html/body/table/tbody/tr[1]/td/table[5]/tbody/tr/td[1]/table/tbody/tr[1]/td[2]/table/tbody/tr[2]/td/table')
  table.rows.each {|x| cliches << x.text}
end


puts cliches
