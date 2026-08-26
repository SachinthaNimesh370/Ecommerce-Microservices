# PowerShell Helper Script for Running Ansible on Windows via Docker or WSL
param (
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AnsibleArgs
)

if ($AnsibleArgs.Count -eq 0) {
    Write-Host "Usage: .\run-ansible.ps1 [ansible / ansible-playbook command]" -ForegroundColor Cyan
    Write-Host "Examples:"
    Write-Host "  .\run-ansible.ps1 ansible all -m ping"
    Write-Host "  .\run-ansible.ps1 ansible-playbook playbooks/site.yml --syntax-check"
    Write-Host "  .\run-ansible.ps1 ansible-playbook playbooks/01-system-setup.yml"
    Write-Host "  .\run-ansible.ps1 ansible-playbook playbooks/site.yml"
    exit 0
}

$CurrentDir = (Get-Item .).FullName
$CommandStr = $AnsibleArgs -join " "

Write-Host "Executing Ansible in container: $CommandStr" -ForegroundColor Green

docker run --rm -i `
  -v "${CurrentDir}:/ansible" `
  -w /ansible `
  -e ANSIBLE_CONFIG=/ansible/ansible.cfg `
  cytopia/ansible `
  $AnsibleArgs
