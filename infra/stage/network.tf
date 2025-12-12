resource "hcloud_network" "authos_network" {
  ip_range = "10.0.0.0/16"
  name     = "authos-net"
}

resource "hcloud_network_subnet" "cp_authos_subnet" {
  network_id = hcloud_network.authos_network.id
  type = "cloud"
  network_zone = "eu-central"
  ip_range = "10.0.1.0/24"
}

resource "hcloud_network_subnet" "worker_authos_subnet" {
  network_id = hcloud_network.authos_network.id
  type = "cloud"
  network_zone = "eu-central"
  ip_range = "10.0.2.0/24"
}
resource "hcloud_network_subnet" "db_authos_subnet" {
  network_id = hcloud_network.authos_network.id
  type = "cloud"
  network_zone = "eu-central"
  ip_range = "10.0.3.0/24"
}

# resource "hcloud_primary_ip" "cp_authos_ip" {
#   assignee_type = "server"
#   auto_delete   = false
#   type          = "ipv4"
#   name = "cp-authos-ip"
#   datacenter = "hel1-dc2"
#
#    lifecycle {prevent_destroy = true}
# }
# resource "hcloud_primary_ip" "wk_authos_ips" {
#   count = var.worker_count
#   datacenter = "hel1-dc2"
#   name = "wk-authos-${count.index + 1}-ip"
#   assignee_type = "server"
#   auto_delete   = false
#   type          = "ipv4"
#
#    lifecycle {prevent_destroy = true}
# }




