import React from 'react';
import {render, screen} from '@testing-library/react';
import Inquiry from './Inquiry';

test('renders learn react link', () => {
    render(<Inquiry/>);
  const linkElement = screen.getByText(/learn react/i);
  expect(linkElement).toBeInTheDocument();
});
