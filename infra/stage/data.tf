data "hcloud_ssh_key" "authos-cluster" {
  name = "authos-cluster"
}

data "hcloud_primary_ip" "cp_authos_ip" {
  name = "cp-authos-ip"
}

data "hcloud_primary_ip" "worker_authos_ips" {
  for_each = toset(var.worker_ip_names)
  name     = each.value
}