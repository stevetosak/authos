resource "hcloud_server" "authos_control_plane" {
  name        = "cp-authos"
  server_type = "cx23"
  image       = "ubuntu-24.04"
  location    = "hel1"
  ssh_keys = [data.hcloud_ssh_key.authos-cluster.id]

  network {
    network_id = hcloud_network.authos_network.id
    ip         = "10.0.1.5"
  }

  public_net {
    ipv6_enabled = false
    ipv4         = data.hcloud_primary_ip.cp_authos_ip.id
  }


  depends_on = [
    hcloud_network_subnet.cp_authos_subnet // morat vaka deka probvit paralelno da kreirat subnet i server
  ]
}

resource "hcloud_server" "workers" {
  count       = var.worker_count
  name        = "wk-authos-${count.index +1}"
  server_type = "cx23"
  location    = "hel1"
  image       = "ubuntu-24.04"
  ssh_keys = [data.hcloud_ssh_key.authos-cluster.id]

  network {
    network_id = hcloud_network.authos_network.id
    ip         = "10.0.2.${5 + count.index + 1}" // worker ips 10.0.2.6 and 10.0.2.7
  }

  dynamic "public_net" {
    for_each = var.assign_public_ips_to_workers ? [1] : []
    content {
      ipv4         = data.hcloud_primary_ip.worker_authos_ips[var.worker_ip_names[count.index]].id
      ipv6_enabled = false
    }
  }

  depends_on = [
    hcloud_network_subnet.worker_authos_subnet
  ]

}

