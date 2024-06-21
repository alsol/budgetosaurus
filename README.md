## Budgetosaurus Telegram Bot


## Local setup
You need to install the required middleware first:
```shell
python3 -m venv ./pyenv \ 
  && source ./pyenv/bin/activate \
  && pip3 install -r ./python/requirements.txt
```
Make sure you have the postgres instance running on your device.  
To run application locally:
```shell
export API_TOKEN=<your tg bot api token> \
   && sbt run
```
You can also bootstrap application via docker-compose:
```shell
sbt docker:publishLocal && docker-compose up -e API_TOKEN=<your tg bot api token>
```