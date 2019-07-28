#!/usr/bin/env ruby

require 'mailchimp'
require 'csv'

arr = CSV.read("humble-beast_2016-06-29_14-02-15.csv")
header = arr.shift

hashes = []

arr.each do |row|
  hashes << Hash[header.zip row]
end

API_KEY = "redacted"
LIST_ID = "redacted"

mailchimp = Mailchimp::API.new(API_KEY)

mailchimp_users = hashes.map do |user|
  [{ "EMAIL" => { "email" => user['Email'],
    #"EUID" => "123",
    #"LEID" => "123123"
  },

  :EMAIL_TYPE => 'html',
  :merge_vars => { "FNAME" => user["Name"],
    "LNAME"  => "",
    "STATUS"    => "Subscribed",
  }
  }]
end

# {"add_count"=>0, "adds"=>[], "update_count"=>1, "updates"=>[{"email"=>"elijahthaprophet116@gmail.com", "euid"=>"ade748e76a", "leid"=>"93542613"}], "error_count"=>0, "errors"=>[]}
#result = mailchimp.lists.batch_subscribe(LIST_ID, example, false, true, false)
#puts result


#emails = [{
#  "email" => {
#    "email" => "elijahthaprophet116@gmail.com",
#    "euid"  => "ade748e76a",
#    "leid"  => "93542613"
#}
#}]
#results = mailchimp.lists.member_info(LIST_ID, emails)


current_members = mailchimp.lists.members(LIST_ID)['data'].map {|x| x['email']}
puts current_members
