class Discounts
  def initialize
    @browser = @browser || Mechanize.new  #{|a| a.log = Logger.new(STDERR) }

  end


end
