# Introduction

This project is a clone of g0v's Hackfoldr. 
It is designed to fit the needs of Padagraph.io an to be more easily customisable.

Written in ScalaJS, all the code runs on the client side

# Getting started 

All you need to start is [sbt](https://scala-sbt.com)

clone the repository

```
$ git clone git@github.com:padagraph/PadagraphFoldr.git
```

go to the root of the repo and run sbt

```
$ cd PadagraphFoldr
$ sbt
```

from sbt shell, run  `fastOptJS` to build the dev version of the js
Or `fullOptJS` for production.


Once the js file is compiled, it will be located in the `target/scala-2.12/` directory. 

# Hosting 

Set the path in the `index.html` file according to your production setup.

For the routes to be analysed properly,  the webserver needs to be configured to serve `index.html` for any subpath of the app (except for static files)

Here is an example of configuration for an nginx server used during development

```
server {
        listen   80;
        server_name  some_name; # (set it in /etc/hosts)
        access_log  /tmp/access.log;
        error_log  /tmp/error.log debug;

        root <path to>/PadagraphFoldr/target/scala-2.12;

        location / {
                # autoindex on;
                try_files $uri $uri/ /classes/index.html;
        }
}

```

TODO: show how to host directly from github pages
