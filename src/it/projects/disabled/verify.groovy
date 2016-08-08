import org.codehaus.plexus.util.FileUtils

def file = new File(basedir, "build.log")
String buildLog = FileUtils.fileRead(file)
return buildLog.contains("gitflow-incremental-builder is disabled.")
