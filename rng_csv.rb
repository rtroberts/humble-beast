#!/usr/bin/env ruby
#

def create_string(prefix)
  "#{prefix}#{Array.new(3) { rand(0...9) }.join('')}"
end


a_list = {}
b_list = {}
c_list = {}

puts "Starting a_list"
until a_list.keys.length == 100 
  a_list[create_string("GCA-")] = true
end
puts "Starting b_list"
until b_list.keys.length == 100 
  b_list[create_string("GCB-")] = true
end
puts "Starting c_list"
until c_list.keys.length == 50 
  c_list[create_string("GCC-")] = true
end

puts a_list.keys
puts b_list.keys
puts c_list.keys
