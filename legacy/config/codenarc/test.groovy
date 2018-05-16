// File: config/codenarc/rules.groovy

ruleset {
  ruleset('rulesets/basic.xml') {
    exclude 'EmptyClass'
  }
  ruleset('rulesets/braces.xml')
  ruleset('rulesets/concurrency.xml')
  ruleset('rulesets/convention.xml') {
    exclude 'NoDef'
  }
  ruleset('rulesets/design.xml') {
    exclude 'PrivateFieldCouldBeFinal'
    exclude 'AbstractClassWithoutAbstractMethod'
  }
  ruleset('rulesets/dry.xml') {
    exclude 'DuplicateNumberLiteral'
    exclude 'DuplicateStringLiteral'
  }
  ruleset('rulesets/enhanced.xml') {
    exclude 'CloneWithoutCloneable'
    exclude 'JUnitAssertEqualsConstantActualValue'
    exclude 'UnsafeImplementationAsMap'
  }
  ruleset('rulesets/exceptions.xml')
  ruleset('rulesets/formatting.xml') {
    exclude 'ClassJavadoc'
    exclude 'SpaceAroundMapEntryColon'
  }
  ruleset('rulesets/generic.xml')
  ruleset('rulesets/groovyism.xml')


  ruleset('rulesets/imports.xml') {
    exclude 'NoWildcardImports'
  }
  ruleset('rulesets/jdbc.xml')
  ruleset('rulesets/logging.xml') {
    exclude 'Println'
    exclude 'PrintStackTrace'
  }
  ruleset('rulesets/naming.xml') {
    exclude 'MethodName'
  }
  ruleset('rulesets/security.xml')
  ruleset('rulesets/serialization.xml') {
    exclude 'SerializableClassMustDefineSerialVersionUID'
  }
  ruleset('rulesets/size.xml') {
    exclude 'AbcMetric'
    exclude 'MethodSize'
  }

  ruleset('rulesets/unnecessary.xml') {
    exclude 'UnnecessaryBooleanExpression'
  }
  ruleset('rulesets/unused.xml')
}