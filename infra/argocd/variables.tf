
variable "github_token" {
  description = "GitHub Personal Access Token"
  type        = string
  sensitive   = true
}

variable "github_user_name" {
  description = "GitHub Username"
  type        = string
  default = "priyanshu124"
}