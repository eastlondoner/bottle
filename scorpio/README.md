Scorpio
========


Setup
-----

You'll need Node.JS installed (we use it for the build scripts, and will use it for the server parts as well). You can get it here: http://nodejs.org/ or install with Homebrew (`brew install nodejs`)

You will also need the latest version of npm. An old version of npm usually comes bundled with node and can be used to bootstrap to the latest version.

    npm install -g npm

Then remove the old version of npm by finding the (old) npm folder and deleting it.

Check npm version using:

    npm version 
    ...
    > npm: '2.1.7' 

You need version 2.x.x    

To install the dev requirements from the project directory run:

    npm install
    sudo npm install -g grunt-cli 
    sudo npm install -g nodemon
    sudo npm install -g bower
    bower install

You'll need to create some configuration files before you can run the server.

In the scorpio directory you need to create a config folder containing serverConfig.json (./config/serverConfig.json).
    {
        "server" : {
            "port": 8000
        }
    }

To start the build watcher and demo server run:

    grunt

Then open in your browser: http://127.0.0.1:8000
