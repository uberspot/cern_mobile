#import "ViewController.h"
#import "WebViewController.h"

@interface ViewController ()


@end

@implementation ViewController

@synthesize label;
@synthesize tableView;

NSMutableArray *groupData;


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    //Add notification observer 
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(receiveNotification:)
                                                 name:@"TestNotification" object:nil];
}

-(void) receiveNotification:(NSNotification *) notification {
    if([[notification name] isEqualToString:@"TestNotification"]) {
        //Retrieve included data from notification
        NSDictionary *contD = notification.userInfo;
        NSString *content = [contD objectForKey:@"contentKey"];
        //NSLog(@"%@", content);
        self.label.text = @"Groups:";
    
        NSError *error = NULL;

        //Parse content and get group names
        NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"<a href=\"mailto:.*cern.ch" options:NSRegularExpressionCaseInsensitive error:&error];
        
        NSArray *arrayOfAllMatches = [regex matchesInString:content options:0 range:NSMakeRange(0, [content length])];
        
        groupData = [[NSMutableArray alloc] init];
        
        for (NSTextCheckingResult *match in arrayOfAllMatches) {
            NSString* substringForMatch = [content substringWithRange:match.range];
            
            [groupData addObject:[[
                        substringForMatch stringByReplacingOccurrencesOfString:@"@cern.ch"
                                   withString:@""] stringByReplacingOccurrencesOfString:@"<a href=\"mailto:" withString:@""]];
        }
        //Display groups in tableview
        [tableView reloadData];
    }
}

-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [tableView reloadData];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (IBAction)doLogin:(id)sender {
    //Called when login button is pressed, opens webview with given url
    NSURL *url = [NSURL URLWithString:@"https://e-groups.cern.ch/e-groups/EgroupsSearchMember.do"];
    WebViewController *webViewController =
    [[WebViewController alloc] initWithURL:url andTitle:@"CERN Login"];
    [self presentViewController:webViewController animated:YES completion:nil];
    
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [groupData count];
}

- (UITableViewCell *)tableView:(UITableView *)ltableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell *cell = [ltableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];
    }
    
    cell.textLabel.text = [groupData objectAtIndex:indexPath.row];
    return cell;
}

@end
