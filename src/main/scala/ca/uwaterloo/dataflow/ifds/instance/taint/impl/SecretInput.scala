package ca.uwaterloo.dataflow.ifds.instance.taint.impl


trait SecretInput extends SecretDefFromConfig {

  override def configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/resources/SecretInput.conf"
}
