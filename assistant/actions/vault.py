import os
from typing import Dict, Text, Any

import hvac

class Vault:
    """class to connect to local Hashicorp Vault"""

    def __init__(self):
        self.vault_client = hvac.Client(
            url=os.environ["VAULT_ADDR"],
            token=os.environ["VAULT_TOKEN"]
        )
    
    def load_secret(
        self,
        name
    ) -> Dict[Text, Any]:
        """Load a v2 secret from the Vault.
        Args:
            name: the name or full path of the secret. 
        Returns:
            Dict containing the secret data.
        """

        secret_response = self.vault_client.secrets.kv.read_secret_version(path=name)
        return secret_response["data"]["data"]
        