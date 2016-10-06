The project.zip file is a zip containing these maven artefacts :

- parent:**parent**:1.0-SNAPSHOT:pom
    - child1:**child1**:1.0-SNAPSHOT
    - child2:**child2**:1.0-SNAPSHOT:pom
        - subchild1:**subchild1**:1.0-SNAPSHOT
        - subchild2:**subchild2**:1.0-SNAPSHOT
    - child3:**child3**:1.0-SNAPSHOT (*dependency* : child6:**child6**:1.0-SNAPSHOT, child7:**child7**:1.0)
    - child4:**child4**:1.0-SNAPSHOT:pom
        - subchild41:**subchild41**:1.0-SNAPSHOT (*parent* : child4:**child4**:1.0-SNAPSHOT)
        - subchild42:**subchild42**:1.0-SNAPSHOT
    - child5:**child5**:1.0-SNAPSHOT:pom
    - child6:**child6**:1.0-SNAPSHOT:jar
    - child7:**child7**:1.0:pom
