# The Secret that ArgoCD uses to "see" your GitHub Repo
resource "kubernetes_secret" "argocd_github_repo" {
  metadata {
    name      = "github-repo-streaming-pipeline"
    namespace = "argocd" # Must be in the same namespace as ArgoCD

    labels = {
      "argocd.argoproj.io/secret-type" = "repository"
    }
  }

  data = {
    type     = "git"
    url      = "https://github.com/priyanshu124/streaming-data-pipeline.git"
    username = var.github_user_name
    password = var.github_token
  }
}
