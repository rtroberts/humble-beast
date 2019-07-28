#!/usr/bin/env ruby

file = File.readlines(ARGV[0])

file.each do |x|
  width = /.*width="(\d+)".*/.match(x)
  height = /.*height="(\d+)".*/.match(x)
  if width
    newwidth = ((width[1].to_f / 700.0) * 100).to_i.to_s
    x.gsub!(/(.*)width="(\d+)"(.*)/, "\\1width=\"#{newwidth}%\"\\3")
  end
  if height
    newheight = ((height[1].to_f / 700.0) * 100).to_i.to_s
    x.gsub!(/(.*)height="(\d+)"(.*)/, "\\1height=\"#{newheight}%\"\\3")
  end
  puts x
end


