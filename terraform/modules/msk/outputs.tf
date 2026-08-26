output "bootstrap_brokers" {
  value = aws_msk_cluster.kafka.bootstrap_brokers
}

output "bootstrap_brokers_sasl_scram" {
  value = aws_msk_cluster.kafka.bootstrap_brokers_sasl_scram
}

output "cluster_arn" {
  value = aws_msk_cluster.kafka.arn
}
