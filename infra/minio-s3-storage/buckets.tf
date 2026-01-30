# --- 3. The S3 Provider (Connecting to Localhost) ---
provider "aws" {
  alias                       = "minio_local"
  access_key                  = var.minio_root_user
  secret_key                  = var.minio_root_password
  region                      = "us-east-1"
  s3_use_path_style           = true 
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
  
  endpoints {
    s3 = var.s3_endpoint
  }
}

# provider "aws" {
#   alias                       = "minio_local"
#   access_key                  = "admin"
#   secret_key                  = "password123"
#   region                      = "us-east-1"
#   s3_use_path_style           = true
#   skip_credentials_validation = true
#   skip_metadata_api_check     = true
#   skip_requesting_account_id  = true
#   endpoints {
#     s3 = "http://localhost:9000"
#   }
# }

# --- 4. The Bucket & Data ---
resource "aws_s3_bucket" "raw" {
  provider   = aws.minio_local
  bucket     = "${var.cluster_name}-raw-data"
  depends_on = [time_sleep.wait_for_minio]
}