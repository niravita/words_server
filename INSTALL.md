Steps to run on docker locally:

1) navigate to the project directory
2) docker build . --tag words_server
3) docker run -p8080:8080 words_server