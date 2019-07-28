#!/usr/bin/env ruby


require 'aws-sdk'
Aws.config.update(:region => 'us-east-1', 
                  :credentials => Aws::Credentials.new('redacted', 'redacted'))

client = Aws::SES::Client.new(region: 'us-east-1')

addresses = ARGV

addresses.each do |address|

email = {
  source: "support@humblebeast.com", # required
  destination: { # required
  to_addresses: [address.strip.to_s],
},
message: { # required
  subject: { # required
  data: "Left Roasters Shutdown", # required
},
body: { # required
  text: {
  data: "Dear Left Customer,
  First of all, we want to thank you for your support of Humble Beast through Left Roasters. Your support has helped us to continue and further our ministry. Over the last few months, we at (Humble Beast) have spent much time and prayer considering our next steps with Left Roasters. Do we continue to grow as a coffee company or do we scale back the necessary workload to sustain our first ministry? 
  Given the unique demands of operating Left Roasters, including the required man-hours, investment capital, marketing, time spent researching and sampling new coffees, and keeping up with the ever-growing subscribers and wholesale accounts; we have collectively decided to re-focus our efforts on more ministry related projects.  These projects include helping to create more Humble Beast albums, educational ministry content, such as books like “Daddy Issues: How the Gospel Heals Wounds Left by Absent, Abusive and Aloof Fathers,” as well as the Canvas Conference. 
  We want to spend all of our efforts and resources in advancing the kingdom of God. We recognize that many of you have supported Left because of the close relationship shared with Humble Beast and we would love for you to continue to support Humble Beast.  You can do this here: https://app.moonclerk.com/pay/eez7pyseooy
  Thank you again for your kind generosity to Humble Beast.
  
  Every blessing,
  Thomas Terry", # required
},
html: {
  data: "Dear Left Customer,<br><p>
  First of all, we want to thank you for your support of Humble Beast through Left Roasters. Your support has helped us to continue and further our ministry. Over the last few months, we at (Humble Beast) have spent much time and prayer considering our next steps with Left Roasters. Do we continue to grow as a coffee company or do we scale back the necessary workload to sustain our first ministry? </p>
<p>Given the unique demands of operating Left Roasters, including the required man-hours, investment capital, marketing, time spent researching and sampling new coffees, and keeping up with the ever-growing subscribers and wholesale accounts; we have collectively decided to re-focus our efforts on more ministry related projects.  These projects include helping to create more Humble Beast albums, educational ministry content, such as books like “Daddy Issues: How the Gospel Heals Wounds Left by Absent, Abusive and Aloof Fathers,” as well as the Canvas Conference.</p> 
<p>We want to spend all of our efforts and resources in advancing the kingdom of God. We recognize that many of you have supported Left because of the close relationship shared with Humble Beast and we would love for you to continue to support Humble Beast.  You can do so <a href='https://app.moonclerk.com/pay/eez7pyseooy'>here</a>.</p><p>Thank you again for your kind generosity to Humble Beast.</p><p>Every blessing,</p><p>Thomas Terry</p>", # required
},
},
},
reply_to_addresses: ["support@humblebeast.com"],
return_path: "support@humblebeast.com",
}

resp = client.send_email(email)
end
