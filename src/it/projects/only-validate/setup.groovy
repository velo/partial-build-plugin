import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.Charset

import com.vackosar.gitflowincrementalbuild.mocks.UnZiper

import static java.nio.charset.StandardCharsets.UTF_8

def project = new File(projectDir as String)
def zip = new File(project, "src/it/project.zip")
new UnZiper().act(zip, basedir as File);

Path path = new File(basedir as File, "pom.xml").toPath()
Charset charset = UTF_8;
String content = new String(Files.readAllBytes(path), charset);
content = content.replaceAll("@project.version@", projectVersion);
Files.write(path, content.getBytes(charset));

return true;