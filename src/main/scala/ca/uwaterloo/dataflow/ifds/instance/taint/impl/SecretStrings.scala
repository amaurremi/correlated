package ca.uwaterloo.dataflow.ifds.instance.taint.impl


trait SecretStrings extends SecretDefFromConfig {

  override def configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/resources/SecretStrings.conf"
}
