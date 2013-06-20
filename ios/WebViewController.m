#import "WebViewController.h"

@interface WebViewController ()

@end

@implementation WebViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (void)viewDidLoad
{  
    [super viewDidLoad];
    webTitle.title = theTitle;
    //Load url in webview
    NSURLRequest *requestObject = [NSURLRequest requestWithURL:theURL];
    [webView loadRequest:requestObject];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (id)initWithURL:(NSURL *)url andTitle:(NSString *)string {
    if( self = [super init] ) {
        theURL = url;
        theTitle = string;
    }
    return self;
}

-(id)initWithURL:(NSURL *)url {
    return [self initWithURL:url andTitle:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    webView.delegate = nil;
    [webView stopLoading]; 
}

- (void)webViewDidStartLoad:(UIWebView *)wv {
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)wv {
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    //Retrieve cookies with session id for wanted url
    NSArray * availableCookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:@"https://e-groups.cern.ch/e-groups/EgroupsSearchMember.do"]];
    NSLog(@"%@", availableCookies);
    if(availableCookies != nil && [availableCookies count] !=0) {
         NSString *content = [webView stringByEvaluatingJavaScriptFromString:@"document.body.outerHTML"];
        
        //Notify viewController and send it the retrieved content
        NSDictionary *site = [NSDictionary dictionaryWithObject:content forKey:@"contentKey"];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"TestNotification"
                                                            object:nil userInfo:site];
        //Close webview
        [webView stopLoading]; 
        [self dismissViewControllerAnimated:YES completion:nil];

    }
}

//Handle and display errors
- (void)webView:(UIWebView *)wv didFailLoadWithError:(NSError *)error {
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    NSString *errorString = [error localizedDescription];
    NSString *errorTitle = [NSString stringWithFormat:@"Error (%d)", error.code];
    UIAlertView *errorView =
    [[UIAlertView alloc] initWithTitle:errorTitle
                               message:errorString delegate:self cancelButtonTitle:nil
                     otherButtonTitles:@"OK", nil]; 
    [errorView show];
}

- (void)didPresentAlertView:(UIAlertView *)alertView {
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
