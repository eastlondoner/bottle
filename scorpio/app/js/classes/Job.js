define("Job", ['underscore'], function () {
    'use strict';
    /**
     * Job class
     */
    function Job(obj) {
        _.extend(this, obj);
        if (!this.id) this.id = this.name;
        if (!this.validate()) {
            throw "Invalid Job Defn";
        }
    }

    Job.prototype.validate = function () {
        var props = _.chain(this).pick(['name', 'id']).values().value();
        var checkTypes = _.every(props, _.isString);
        var checkNotEmpty = !_.some(props, _.isEmpty);
        return checkTypes && checkNotEmpty;
    };

    Job.build = function (data) {
        return new Job(data);
    };

    return Job;
});
