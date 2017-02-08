class Outputter:
    def __init__(self, width=80, mute=False):
        self.width = width
        self.column_widths = []
        self.mute=mute
        
    def print_heading(self, text, with_gap=True):
        if not self.mute:
            if (with_gap):
                self.print_gap()
            self.print_text(text)
            self.print_horizontal_rule()
        
    def print_gap(self):
        if not self.mute:
            print("")
        
    def print_text(self, text):
        if not self.mute:
            print(text)
       
    def print_horizontal_rule(self):
        if not self.mute:
            print("-" * self.width)
        
    # --------------------------------------------------------------------------
    
    def print_columns_headers(self, *headings):
        if not self.mute:
            self.print_horizontal_rule()
            if len(headings) > 0:
                self.print_columns(*headings)
                self.print_horizontal_rule()
        
    def print_columns_footers(self, *footers):
        if not self.mute:
            self.print_horizontal_rule()
            if len(footers) > 0:
                self.print_columns(*footers)
                self.print_horizontal_rule()
        
    def print_columns(self, *columns):
        if not self.mute:
            text_parts = []
            for i in range(0, len(columns)):
                col_text  = columns[i]
                col_width = self.column_widths[i] if i < len(self.column_widths) else 0
                
                if col_width > 0:
                    text_parts.append("%-*s" % (col_width, col_text))
                else:
                    text_parts.append("%s" % col_text)
                    
            print("".join(text_parts)) 
        
    def set_column_widths(self, *column_widths):
        self.column_widths = column_widths
        
        
