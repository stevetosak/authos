variable "HCLOUD_TOKEN" {
  sensitive = true
}
variable "worker_count" {
  type        = number
  nullable    = false
  default     = 2
  description = "Number of worker servers to create"
}
variable "assign_public_ips_to_workers" {
  type        = bool
  default     = true
  description = "Whether to assign public ip to worker nodes"
}

variable "worker_ip_names" {
  type    = list(string)
  default = ["wk-authos-1-ip", "wk-authos-2-ip"]
}


