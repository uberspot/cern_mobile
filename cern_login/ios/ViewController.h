#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>

@property (strong, nonatomic)IBOutlet UILabel *label;

@property(nonatomic, strong) IBOutlet UITableView *tableView;

- (IBAction)doLogin:(id)sender;

@end

