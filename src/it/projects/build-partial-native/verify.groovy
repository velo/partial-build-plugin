import org.codehaus.plexus.util.FileUtils

def file = new File(basedir, "build.log")
String buildLog = FileUtils.fileRead(file)

boolean verified = true
verified &= buildLog.contains(" subchild2")
verified &= buildLog.contains(" child3")
verified &= buildLog.contains(" child4")
verified &= buildLog.contains(" subchild41")

verified &= !buildLog.contains(" child1")
verified &= !buildLog.contains(" child2")
verified &= !buildLog.contains(" child5")
verified &= !buildLog.contains(" child6")
verified &= !buildLog.contains(" child7")
verified &= !buildLog.contains(" subchild1")
verified &= !buildLog.contains(" subchild42")

verified &= buildLog.contains(" BUILD SUCCESS")

return verified;
