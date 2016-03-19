package tdunnick.jphineas.config;

public class LogConfig extends XmlConfig
{
  public String getLogId ()
  {
  	return getValue ("LogId");
  }
  public String getLogName ()
  {
  	return getValue ("LogName");
  }
  public String getLogLevel ()
  {
  	return getValue ("LogLevel");
  }
  public String getLogLocal ()
  {
  	return getValue ("LogLocal");
  }
  public String getLogDays ()
  {
  	return getValue ("LogDays");
  }
}
