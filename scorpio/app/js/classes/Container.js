define("Container",['underscore'], function () {
    'use strict';
    /**
     * Container class
     */
    function Container(obj) {
        _.extend(this, obj);
        if (!this.id) this.id = this.name;

        if (!this.validate()) {
            throw "Invalid Container Defn";
        }
    }

    Container.prototype.validate = function () {
        var props = ['id','name'];
        var checkTypes = _.every(props, _.isString);
        var checkNotEmpty = !_.some(props, _.isEmpty);
        return checkTypes && checkNotEmpty;
    };

    Container.build = function(data){
        return new Container(data);
    };

    return Container;
});
