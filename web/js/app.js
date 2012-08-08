/******************************************************/
/*				INITS			            		  */
/******************************************************/
var HaBlog = Em.Application.create({
    ready:function () {
        this._super();

        HaBlog.GetItemsFromServer();
    }
});


HaBlog.GetItemsFromServer = function () {
    $.getJSON("../domain/example.json",
        function (data) {
            console.log(data);
        }
    );
}
/******************************************************/
/*				MODEL								  */
/******************************************************/

// POST ITEM
HaBlog.Post = Em.Object.extend({
    uid:null,
    title:null,
    headline:null,
    state:null,
    author:null,
    created:moment().subtract('years', 100),
    summary:null,
    sections:null,
    tags:null,
    rating:null,
    comments:null
});

// SECTION ITEM
HaBlog.Section = Em.Object.extend({
    text:null,
    headline:null
});

HaBlog.Comment = Em.Object.extend({
    user:null,
    text:null,
    created:moment().subtract('years', 100),
    rating:null,
    replies:null
});

/******************************************************/
/*				CONTROLLERS							  */
/******************************************************/

// POST LIST CONTROLLER
HaBlog.postsController = Em.ArrayController.create({
    //container with the list of posts
    content:[],

    //add a new post to the list checking that it was not previously there and ordered by creationg date
    addPost:function (post) {
        // Check to see if there are any post in the controller with the same uid already
        var exists = this.filterProperty('uid', post.uid).length;
        if (exists === 0) {
            // If no results are returned, we insert the new item into the data controller in order of publication date
            var length = this.get('length'), idx;
            idx = this.binarySearch(Date.parse(post.get('created')), 0, length);
            this.insertAt(idx, post);
            return true;
        }
    },
    // Binary search implementation that finds the index where a entry
    // should be inserted when sorting by date.
    binarySearch:function (value, low, high) {
        var mid, midValue;
        if (low === high) {
            return low;
        }
        mid = low + Math.floor((high - low) / 2);
        midValue = Date.parse(this.objectAt(mid).get('created').get('time'));

        if (value < midValue) {
            return this.binarySearch(value, mid + 1, high);
        } else if (value > midValue) {
            return this.binarySearch(value, low, mid);
        }
        return mid;
    }
});

/******************************************************/
/*				VIEWS								  */
/******************************************************/
// View for the Post list
HaBlog.PostSummaryListView = Em.View.extend({
    tagName:'article',
    template:'post-list-view',
    // Returns the creation date
    creationDay:function () {
        var d = this.get('content').get('created');
        return moment(d).format('DD');
    }.property('HaBlog.postsController.@each.created'),
    creationMonth:function () {
        var d = this.get('content').get('created');
        return moment(d).format('MMM');
    }.property('HaBlog.postsController.@each.created'),
    creationYear:function () {
        var d = this.get('content').get('created');
        return moment(d).format('YYYY');
    }.property('HaBlog.postsController.@each.created'),
    toggleButtons:function (event, view) {
        this.$('.fullText').fadeToggle("400", "linear", function () {
            $(this).closest(".post").find(".toggleButton").toggle();
        });
    }
});

