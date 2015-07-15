//
//  ViewController.m
//  CERNCertInstaller
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)doInstallation:(id)sender {
    [self showPopup: [self installCert:@"cern_root_ca" andExtension:@"cer"] withTitle:@"cern root ca cert"];
    [self showPopup: [self installCert:@"cern_root_ca2" andExtension:@"crt"] withTitle:@"cern root ca 2 cert"];
    [self showPopup: [self installCert:@"cern_grid_ca" andExtension:@"crt"] withTitle:@"cern grid ca cert"];
    [self showPopup: [self installCert:@"cern_trusted_ca" andExtension:@"cer"] withTitle:@"cern trusted ca cert"];
    
}

- (OSStatus)installCert:(NSString*)name andExtension:(NSString*) extension {
    NSString *rootCertPath = [[NSBundle mainBundle] pathForResource:@"cern_root_ca" ofType:@"cer"];
    NSData *rootCertData = [NSData dataWithContentsOfFile:rootCertPath];
    
    OSStatus            err = noErr;
    SecCertificateRef   cert;
    
    cert = SecCertificateCreateWithData(NULL, (__bridge CFDataRef) rootCertData);
    
    CFTypeRef result;
    
    NSDictionary* dict = [NSDictionary dictionaryWithObjectsAndKeys:
                          (__bridge id)kSecClassCertificate, kSecClass,
                          cert, kSecValueRef,
                          nil];
    
    err = SecItemAdd((__bridge CFDictionaryRef)dict, &result);
    
    CFRelease(cert);
    return err;
}

- (void)showPopup:(OSStatus) error withTitle:(NSString*) titleString {
    NSString *errorMessage;
    if( error == noErr) {
        errorMessage = @"Certificate installation successfull" ;
    } else if( error == errSecDuplicateItem ) {
        errorMessage = @"Duplicate certificate entry" ;
    } else {
        errorMessage = @"Certificate installation failure" ;
    }
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle: titleString message:errorMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
    //[alert release];
}
@end
