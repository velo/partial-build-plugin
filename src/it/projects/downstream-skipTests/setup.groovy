import com.lesfurets.maven.partial.mocks.UnZiper

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static java.nio.charset.StandardCharsets.UTF_8

def project = new File(sourceDir as String)
def zip = new File(project, "src/it/project.zip")
new UnZiper().act(zip, basedir as File);

folder = "downstream-skipTests"
Files.copy(Paths.get(sourceDir as String, "src/it/projects", folder, "pom.xml"),
        Paths.get(basedir as String, "pom.xml"),
        StandardCopyOption.REPLACE_EXISTING)

Path path = new File(basedir as File, "pom.xml").toPath()
String content = new String(Files.readAllBytes(path), UTF_8);
content = content.replaceAll("@project.version@", projectVersion);
Files.write(path, content.getBytes(UTF_8));

return true;