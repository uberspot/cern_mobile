//
//  ViewController.h
//  CERNCertInstaller
//
//  Created by Pawel Sarbinowski local on 7/4/13.
//  Copyright (c) 2013 Pawel Sarbinowski local. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

- (IBAction)doInstallation:(id)sender;
- (OSStatus)installCert:(NSString*)name andExtension:(NSString*) extension;
- (void)showPopup:(OSStatus) error withTitle:(NSString*) titleString;

@end
