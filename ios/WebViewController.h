#import <UIKit/UIKit.h>


@interface WebViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate> {
    NSURL *theURL;
    NSString *theTitle;
    IBOutlet UIWebView *webView;
    IBOutlet UINavigationItem *webTitle;
}

- (id) initWithURL: (NSURL *) url;
- (id) initWithURL:(NSURL *)url andTitle:(NSString *) string;

@end
