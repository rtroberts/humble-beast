#!/usr/bin/env ruby
require 'csv'
require 'gibbon'

APIKey = "redacted"
FromMailingList = "redacted"
ToList = "redacted"

Gibbon::Request.api_key = APIKey
Gibbon::Request.timeout = 15
Gibbon::Request.open_timeout = 15
Gibbon::Request.symbolize_keys = true
Gibbon::Request.debug = false
gibbon = Gibbon::Request.new()

listlength = 1
offset = 0

userlist = []

CSV.open("list_members.csv", "a") do |csv|
  csv << ["email_address", "FNAME", "LNAME", "ZIP"]
end

while listlength > 0
  unsubs = gibbon.lists(FromMailingList).members.retrieve(params: {"count": "50", "offset": offset, "status": "subscribed"})
  unsubs.body[:members].each do |x|
    zipmatch = x[:merge_fields].values.collect {|v| v.to_s.match(/^\d{5}(?:[-\s]\d{4})?$/)}.compact
    if zipmatch.empty?
      zip = nil
    else
      zip = zipmatch.first.to_s
    end
    #userlist << [:email => x[:email_address], :first => x[:merge_fields][:FNAME], :last => x[:merge_fields][:LNAME], :zip => zip]
    userlist << [x[:email_address], x[:merge_fields][:FNAME], x[:merge_fields][:LNAME], zip]
  end

  CSV.open("list_members.csv", "a") do |csv|
    userlist.each do |u|
      csv << u
    end
  end

  listlength = unsubs.body[:members].length
  # unsub them and add those to offset
  offset += listlength
  puts "Current offset: #{offset}"
end

