# Rasa Advanced Workshop: Custom Actions and Forms 

IT Helpdesk AI assistant that showcases custom actions and forms integrated with ServiceNow (Snow). The ServiceNow REST API is used
to open incidents and check on latest incident statuses. 

> Note that this repository contains a modified version of [socketio.py](https://github.com/RasaHQ/rasa/blob/master/rasa/core/channels/socketio.py) in order to parse custom metadata.

## Setup

### Install the dependencies

In a Python3 virtual environment run:

```bash
pip install -r requirements.txt
```

### Connect to a ServiceNow instance

To connect to ServiceNow, you can get your own free Developer instance to test this with [here](https://developer.servicenow.com/app.do#!/home)

Collect the following information to add to Vault

- `instance` - This is the address of the ServiceNow developer instance, you don't need the leading https.

- `user` - The username of the service account for the ServiceNow developer instance

- `pwd` - The password of the service account for the ServiceNow developer instance

## Connect to Vault

To connect to Vault, you can [download](https://www.vaultproject.io/downloads) and run a local dev instance.

Open a terminal window and run Vault in dev mode:

```bash
vault server -dev
```

Take note of the VAULT_ADDR and Root token.

In another window, load the ServiceNow instance details that you previously collected into Vault:

```bash
export VAULT_ADDR=<address>
export VAULT_TOKEN=<root_token>
vault kv put secret/snow_service user=<user> pwd=<pwd> instance=<instance>
```

## Running the bot

Use `rasa train` to train a model.

Next set up your action server in another window:

```bash
export VAULT_ADDR=<address>
export VAULT_TOKEN=<token>
rasa run actions
```

Then to talk to the bot, run:

```bash
rasa shell --debug
```

Note that `--debug` mode will produce a lot of output meant to help you understand how the bot is working
under the hood. You can also add this flag to the action server command. To simply talk to the bot, you can remove this flag.

## Testing the bot

You can test the bot on the test conversations by running  `rasa test`.
This will run [end-to-end testing](https://rasa.com/docs/rasa/user-guide/testing-your-assistant/#end-to-end-testing) on the conversations in `tests/conversation_tests.md`.
