A selection of objects that improve my life (at least when I'm writing Swing
apps), along with templates for application-specific objects.

Although I use Maven for the build, I don't use the Maven directory structure.
Instead, there are four top-level source directories:

    src      - source code for classes that will end up in the library
    test     - test code for classes in src; generally resides in the
               same package as the class being tested
    example  - example code, showing the use of classes in a live app;
               all examples live in the net.sf.swinglib.example package
               or a sub-package
    template - template objects, that would be copied for use; all live
               in the net.sf.swinglib.template package
