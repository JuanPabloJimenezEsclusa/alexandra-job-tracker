IA_C ?= terraform

#################################################################################################
# Env & defaults
#################################################################################################

ifneq (,$(wildcard .env))
  include .env
  export
endif

GIT_SHA             := $(shell git rev-parse --short HEAD 2>/dev/null || echo "latest")
IMAGE_TAG           ?= git-$(GIT_SHA)
REGION              ?= eu-west-1
AWS_ACCOUNT         ?= $(shell aws sts get-caller-identity --query Account --output text 2>/dev/null)
ECR_REPO            ?= ajt-serverless
IMAGE_URI           ?= $(AWS_ACCOUNT).dkr.ecr.$(REGION).amazonaws.com/$(ECR_REPO):$(IMAGE_TAG)
ECR_REGISTRY        ?= $(AWS_ACCOUNT).dkr.ecr.$(REGION).amazonaws.com

#################################################################################################
# Prerequisite checks
#################################################################################################

__require = @if ! command -v $(1) >/dev/null 2>&1; then \
  echo "Error: '$(1)' is required but not found. Install it first."; \
  echo "  docker:  https://docs.docker.com/get-docker/"; \
  echo "  aws:     https://aws.amazon.com/cli/"; \
  echo "  java:    https://jdk.java.net/25/"; \
  exit 1; \
  fi

.PHONY: prereqs
prereqs:
	$(call __require,docker)
	$(call __require,aws)
	$(call __require,java)

.PHONY: all build push migrate infra deploy destroy test info rollback help

all: deploy

#################################################################################################
# Image lifecycle
#################################################################################################

build: prereqs
	aws ecr-public get-login-password --region us-east-1 2>/dev/null | \
	  docker login --username AWS --password-stdin public.ecr.aws || true
	docker build -f Dockerfile.native -t $(ECR_REPO):$(IMAGE_TAG) ../..

push: build
	aws ecr get-login-password --region $(REGION) | \
	  docker login --username AWS --password-stdin $(ECR_REGISTRY)
	docker tag $(ECR_REPO):$(IMAGE_TAG) $(IMAGE_URI)
	docker push $(IMAGE_URI)

#################################################################################################
# Database
#################################################################################################

migrate: prereqs
	@if [ -z "${NEON_PASSWORD}" ]; then echo "NEON_PASSWORD is required"; exit 1; fi
	SPRING_PROFILES_ACTIVE=aws,db-migrate \
	  NEON_PASSWORD="${NEON_PASSWORD}" \
	  java -jar ../../bootstrap-server/target/bootstrap-server-*.jar

#################################################################################################
# Infrastructure
#################################################################################################

infra: prereqs
ifeq ($(IA_C),terraform)
	cd terraform && IMAGE_URI=$(IMAGE_URI) ./init-aws-plan.sh && ./apply-aws-plan.sh
else ifeq ($(IA_C),cloud-formation)
	cd cloud-formation && IMAGE_URI=$(IMAGE_URI) ./start.sh
else
	$(error IA_C must be "terraform" or "cloud-formation", got "$(IA_C)")
endif

deploy: prereqs push migrate infra test

destroy: prereqs
ifeq ($(IA_C),terraform)
	cd terraform && ./destroy-aws-plan.sh
else ifeq ($(IA_C),cloud-formation)
	cd cloud-formation && ./stop.sh
else
	$(error IA_C must be "terraform" or "cloud-formation", got "$(IA_C)")
endif

#################################################################################################
# Operations
#################################################################################################

test:
	curl -s https://ajt.jpje.net/api/actuator/health | jq .

info:
	aws lambda get-function-configuration --function-name ajt-serverless --region $(REGION)
	@echo "---"
	aws lambda list-aliases --function-name ajt-serverless --region $(REGION) --query 'Aliases[*].{Name:Name,Version:FunctionVersion,Routing:RoutingConfig}'

rollback:
	@if [ -z "$(ROLLBACK_TO)" ]; then echo "Usage: make rollback ROLLBACK_TO=<version>"; exit 1; fi
	aws lambda update-alias --function-name ajt-serverless --name live \
	  --function-version $(ROLLBACK_TO) --region $(REGION)
	@echo "Alias 'live' now points to version $(ROLLBACK_TO)"

#################################################################################################
# Help
#################################################################################################

help:
	@echo "Usage: IA_C={terraform|cloud-formation} make <target>"
	@echo ""
	@echo "Targets:"
	@echo "  deploy            Full pipeline: build -> push -> migrate -> infra -> test"
	@echo "  build             Build native-image Docker image"
	@echo "  push              Push image to ECR"
	@echo "  migrate           Run Flyway migrations (one-shot)"
	@echo "  infra             Deploy/update infrastructure (via selected IaC)"
	@echo "  destroy           Tear down infrastructure"
	@echo "  test              Smoke test the API health endpoint"
	@echo "  info              Show Lambda config and alias status"
	@echo "  rollback          make rollback ROLLBACK_TO=<N>"
	@echo ""
	@echo "Variables:"
	@echo "  IA_C              IaC tool: terraform (default) or cloud-formation"
	@echo "  IMAGE_TAG         Docker image tag (default: git-<sha>)"
	@echo "  REGION            AWS region (default: eu-west-1)"
