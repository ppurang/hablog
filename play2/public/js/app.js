/******************************************************/
/*				INITS			            		  */
/******************************************************/
var HaBlog = Em.Application.create({
});

HaBlog.ready = function(){

}


/******************************************************/
/*				CONSTANTS							  */
/******************************************************/

HaBlog.CONSTANTS = {
    PATH_CONTEXT : 'http://tech.piyush.purang.net',
    PATH_BLOG_LIST : '/blog'
};

HaBlog.Utilities = {
    Showdown : null
}


HaBlog.GetItemsFromServer = function () {
    $.ajax({
        url:HaBlog.CONSTANTS.PATH_CONTEXT+HaBlog.CONSTANTS.PATH_BLOG_LIST,
        async:false,
        dataType:'json',
        success: function(data) {
            // Use map to iterate through the items and create a new JSON object for
            // each item
            data.map(function(item) {

                var post = HaBlog.CreatePostFromJSon(item);

                HaBlog.postListController.addPost(post);
            });
            console.log("Finished loading");
    }
    });
}

HaBlog.CreatePostFromJSon = function (item){
    //console.log(item);

    var post = HaBlog.Post.create();

    post.set('uid', item.uid);
    post.set('headline', HaBlog.Utilities.Showdown.makeHtml(item.headline.content));
    post.set('title', HaBlog.Utilities.Showdown.makeHtml(item.title.content));
    post.set('author', item.author);
    post.set('summary', HaBlog.Utilities.Showdown.makeHtml(item.summary.content));
    post.set('sections', HaBlog.ParseSections(item.content));
    post.set('tags', item.tags);
    post.set('comments', HaBlog.ParseComments(item.comments));
    post.set('created', moment(item.created.time));

    return post;
}


HaBlog.InitializeMarkdownParser = function () {
    HaBlog.Utilities.Showdown = new Showdown.converter();
};

HaBlog.ParseSections = function(jsonContent){
    var sections = [];

    jsonContent.map(function(item){
        var section = HaBlog.Section.create({
            text: item.text === undefined ? null : item.text.content === undefined ? null : HaBlog.Utilities.Showdown.makeHtml(item.text.content),
            headline : item.headline === undefined ? null : item.headline.content === undefined ? null : HaBlog.Utilities.Showdown.makeHtml(item.headline.content)

        });
        sections.push(section);
    });
    return sections;
}

HaBlog.ParseComments = function (jsonContent){
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
            replies: HaBlog.ParseComments(item.replies),
            totalReplies: function(){
                var total = 0;
                var commentReplies = this.get('replies')
                var repliesLength = commentReplies.length
                for (var i = 0; i < repliesLength; ++i) {
                    if (i in commentReplies) {
                        var reply = commentReplies[i];
                        total = total + reply.get('totalReplies') + 1;
                    }
                }
                return total;
            }.property()
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
    created:null,
    creationDay: function (){
        return moment(this.get('created')).format('DD');
    }.property('created'),
    creationMonth: function (){
        return moment(this.get('created')).format('MMM');
    }.property('created'),
    creationYear:function (){
        return moment(this.get('created')).format('YYYY');
    }.property('created'),
    summary:null,
    sections:null,
    tags:[],
    rating:null,
    comments:null,
    commentsCount: function(){
        var total = 0;
        var commentsArray = this.get('comments');
        var commentsLength = commentsArray.length;
        for (var i = 0; i < commentsLength; ++i) {
            if (i in commentsArray) {
                var comment = commentsArray[i];
                total = total + comment.get('totalReplies') + 1;
            }
        }
        return total;
    }.property('comments')
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
    created: moment().subtract('years', 100),
    createdAgo: function(){
        return (this.get('created').fromNow());
    }.property('created'),
    rating:null,
    replies:[],
    totalReplies:0
});

// RATING COMPONENT
HaBlog.Rating = Em.Object.extend({
    likes:0,
    dislikes:0
})

/******************************************************/
/*				CONTROLLERS							  */
/******************************************************/

// Define the main application controller. This is automatically picked up by
// the application and initialized.
HaBlog.ApplicationController = Ember.Controller.extend({

});

// POST LIST CONTROLLER
HaBlog.PostListController = Ember.ArrayController.extend({
    //container with the list of posts
    content:[],

    //add a new post to the list checking that it was not previously there and ordered by creationg date
    addPost:function (post) {
        //console.log(post);
        // Check to see if there are any post in the controller with the same uid already
        var exists = this.filterProperty('uid', post.uid).length;
        if (exists === 0) {
            // If no results are returned, we insert the new item into the data controller in order of publication date
            var length = this.get('length'), idx;
            idx = this.binarySearch(Date.parse(post.created), 0, length);
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
        midValue = Date.parse(this.objectAt(mid).created);

        if (value < midValue) {
            return this.binarySearch(value, mid + 1, high);
        } else if (value > midValue) {
            return this.binarySearch(value, low, mid);
        }
        return mid;
    }
});

// Define the main application controller. This is automatically picked up by
// the application and initialized.
HaBlog.PostController = Ember.ObjectController.extend({
});

/******************************************************/
/*				VIEWS								  */
/******************************************************/
// View for the Post list
HaBlog.PostListView = Em.View.extend({
    templateName:'postList'
});

//View for the single Post
HaBlog.PostView = Em.View.extend({
    templateName:'post'
});

HaBlog.ApplicationView = Ember.View.extend({
    templateName: 'application',
    //Theme UI initialization if needed after the application view rendered the basic template
    didInsertElement: function(){
        theme_initialize();
    }
});


/******************************************************/
/*				ROUTER								  */
/******************************************************/

HaBlog.Router = Ember.Router.extend({
    root: Ember.Route.extend({
        index: Ember.Route.extend({
            route: '/',
            redirectsTo: 'posts'
        }),
        showMain: function(router, context) {
            console.log("routing to main page");
            router.transitionTo('posts');
        },
        posts: Ember.Route.extend({
            route: '/posts',
            showPost: Ember.Route.transitionTo('post'),
            connectOutlets: function(router) {
                console.log("routing to post list");
                router.get('applicationController').connectOutlet('postList');
            }
        }),
        post: Ember.Route.extend({
            route: '/posts/:uid',
            connectOutlets: function(router, post) {
                console.log("redirecting to post")
                var targetPost = HaBlog.postListController.findProperty('uid', post.uid);
                if (Ember.none(targetPost)){
                    //go to main page if the post doesn't exist
                    router.transitionTo('posts');
                }else{
                    router.get('applicationController').connectOutlet('post', targetPost);
                }
            }
        })
    })
});


$(function() {
    HaBlog.InitializeMarkdownParser();
    HaBlog.postListController = HaBlog.PostListController.create();
    HaBlog.GetItemsFromServer();
    HaBlog.initialize();
});
