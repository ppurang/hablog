/******************************************************/
/*				INITS			            		  */
/******************************************************/
var HaBlog = Em.Application.create({
    ready:function () {
        this._super();



        HaBlog.initializeMarkdownParser();

        HaBlog.GetItemsFromServer();
    }
});


/******************************************************/
/*				CONSTANTS							  */
/******************************************************/

HaBlog.CONSTANTS = {
    PATH_CONTEXT : 'http://localhost:9000',
    PATH_BLOG_LIST : '/blog'
};


HaBlog.GetItemsFromServer = function () {
    $.getJSON(HaBlog.CONSTANTS.PATH_CONTEXT+HaBlog.CONSTANTS.PATH_BLOG_LIST,
        function(data) {
            // Use map to iterate through the items and create a new JSON object for
            // each item
            data.map(function(item) {
                console.log(item);

                var post = {};

                post.uid = item.uid;
                post.headline = item.headline.content;
                post.title = item.title.content;
                post.author = item.author;
                post.summary = item.summary.content;
                post.sections = parseSections(item.content);
                post.tags = item.tags;
                post.comments = parseComments(item.comments);


                console.log("Adding new post");
                console.log(post);
                var emberPost = HaBlog.Post.create(post);
                HaBlog.postsController.addPost(emberPost);

            });
    });
}


HaBlog.initializeMarkdownParser = function() {

}

HaBlog.ParseSections = function(jsonContent){
    var sections = [];
    jsonContent.map(function(item){
        var section = HaBlog.Section.create({
            text: item.text === undefined ? null : item.text.content === undefined ? null : item.text.content,
            headline : item.headline === undefined ? null : item.headline.content === undefined ? null : item.headline.content

        });
        sections.push(section);
    });
    return sections;
}

function parseComments(jsonContent){
    var comments = [];
    jsonContent.map(function(item){
        var comment = HaBlog.Comment.create({
            uid:item.uid,
            text:item.text,
            created: item.created.time === undefined ? moment().subtract('years', 100) : moment(item.created.time),
            rating: HaBlog.Rating.create({
                likes: item.rating === undefined ? 0 : item.rating.likes === undefined ? 0 : item.rating.likes,
                dislikes: item.rating === undefined ? 0 : item.rating.dislikes === undefined ? 0 : item.rating.dislikes
            }),
            replies: item.replies
        });
        comments.push(comment);
    });

    return comments;
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

// COMMENT ITEM
HaBlog.Comment = Em.Object.extend({
    user:null,
    text:null,
    created:moment().subtract('years', 100),
    rating:null,
    replies:null
});

// RATING COMPONENT
HaBlog.Rating = Em.Object.extend({
    likes:0,
    dislikes:0
})

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
        midValue = Date.parse(this.objectAt(mid).get('created'));

        if (value < midValue) {
            return this.binarySearch(value, mid + 1, high);
        } else if (value > midValue) {
            return this.binarySearch(value, low, mid);
        }
        return mid;
    },
    commentsCount : function() {
        return this.get('comments').get('length');
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
    },
    // A 'property' that returns the count of items
    commentsCount: function() {
        return(this.get('content').get('comments').get('length'));
    }.property('HaBlog.postsController.@each.comments')
});

