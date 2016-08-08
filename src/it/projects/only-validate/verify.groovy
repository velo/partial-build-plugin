import org.codehaus.plexus.util.FileUtils

def file = new File(basedir, "build.log")
String buildLog = FileUtils.fileRead(file)

boolean verified = true
verified &= buildLog.contains("Executing validate goal only.")

verified &= buildLog.contains(" BUILD SUCCESS")

return verified;
